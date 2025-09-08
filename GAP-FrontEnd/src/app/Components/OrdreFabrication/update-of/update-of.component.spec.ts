import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateOFComponent } from './update-of.component';

describe('UpdateOFComponent', () => {
  let component: UpdateOFComponent;
  let fixture: ComponentFixture<UpdateOFComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateOFComponent]
    });
    fixture = TestBed.createComponent(UpdateOFComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
