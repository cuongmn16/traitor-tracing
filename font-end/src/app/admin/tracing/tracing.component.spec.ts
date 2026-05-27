import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TracingComponent } from './tracing.component';

describe('TracingComponent', () => {
  let component: TracingComponent;
  let fixture: ComponentFixture<TracingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TracingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TracingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
