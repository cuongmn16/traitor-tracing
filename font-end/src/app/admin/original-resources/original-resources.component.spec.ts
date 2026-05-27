import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OriginalResourcesComponent } from './original-resources.component';

describe('OriginalResourcesComponent', () => {
  let component: OriginalResourcesComponent;
  let fixture: ComponentFixture<OriginalResourcesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OriginalResourcesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OriginalResourcesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
